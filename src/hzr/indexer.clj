(ns hzr.indexer
  (:require [clojure.java.io :as io]
            [hzr.audio :as audio]
            [hzr.fingerprint :as fingerprint])
  (:import [java.util Arrays]))

(def chunk-size 8192)

;; hash of fingerprint to list of songs
;;; the contents of the index are: {fingerprint [{offset song-name}*]*}
(def fingerprint-index (atom {}))

(defn fingerprint-audio-stream [in]
  (let [buffer (make-array Byte/TYPE chunk-size)]
    (loop [bc (.read in buffer)
           fingerprints []]
      (if (= bc -1)
        fingerprints
        (recur (.read in buffer) (conj fingerprints (fingerprint/fingerprint buffer)))))))

(defn fingerprint-audio-file [file]
  (audio/decode-audio-file file fingerprint-audio-stream))

(defn load-audio-file-index [file]
  (let [audio-file (io/file file)
        index-file (io/file (str "index/" (.getName audio-file) ".idx"))]
    (if (.exists index-file)
      (read-string (slurp index-file))
      (let [fprints (fingerprint-audio-file audio-file)
            fingerprint-data {:filename (.getPath audio-file) :fingerprints fprints}]
        (spit index-file fingerprint-data)
        fingerprint-data))))

(defn index-audio-file [file]
  (let [fingerprint-data (load-audio-file-index file)
        filename (keyword (:filename fingerprint-data))
        fps (:fingerprints fingerprint-data)
        index-data (map-indexed (fn [idx fp] {fp {idx filename}}) fps)]
    (swap! fingerprint-index #(apply merge-with conj % index-data))))

(defn index-audio-dir [dir]
  (->> (filter #(.isFile %) (file-seq (io/file dir)))
       (map index-audio-file)))

(defn map-count [map key]
  (assoc map key (inc (get map key 0))))

(defn match-stream [in filename]
  (let [buffer (make-array Byte/TYPE chunk-size)]
    (loop [bc (.read in buffer 0 chunk-size)
           pos 0
           matches {}]
      (if (or (= bc -1) (> pos 10000))
        [pos matches]
        (let [fp (fingerprint/fingerprint buffer)
              ms (get @fingerprint-index fp)
              offms (map (fn [[ts nm]] [(- ts pos) nm]) ms)
              newmatches (reduce map-count matches offms)]
          (if (some #(> % 20) (vals newmatches))
            [pos newmatches]
            (recur (.read in buffer)
                   (inc pos)
                   newmatches)))))))
