(ns hzr.indexer
  (:require [clojure.java.io :as io]
            [hzr.audio :as audio]
            [hzr.fingerprint :as fingerprint])
  (:import [java.io ByteArrayInputStream]
           [java.util Arrays]))

(def chunk-size 8192)

;; hash of fingerprint to list of songs
;;; the contents of the index are: {fingerprint [{offset song-name}*]*}
(def fingerprint-index (atom {}))

;; fingerprinting

(defn fingerprint-stream [^java.io.InputStream in]
  (let [buffer (byte-array chunk-size)]
    (loop [bc (.read in buffer)
           fingerprints []]
      (if (= bc -1)
        fingerprints
        (recur (.read in buffer) (conj fingerprints (fingerprint/fingerprint buffer)))))))

(defn add-to-index [file in]
  (let [fingerprint-data (fingerprint-stream in)
        filename (keyword (.getPath file))
        index-data (map-indexed (fn [idx fp] {fp {idx filename}}) fingerprint-data)]
    (swap! fingerprint-index #(apply merge-with conj % index-data))))

;; matching

(defn map-count [map key]
  (assoc map key (inc (get map key 0))))

(defn match-stream [^java.io.InputStream in]
  (let [buffer (make-array Byte/TYPE chunk-size)]
    (loop [bc (.read in buffer)
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

;; search a directory for duplicate mp3s

(defn match-and-index-file [filename]
  (println "Matching " filename)
  (let [audio-data (audio/decoded-audio-file filename)
        [pos matches] (match-stream (ByteArrayInputStream. audio-data))]
    (add-to-index filename (ByteArrayInputStream. audio-data))
    (if (some #(> % 20) (vals matches))
      (println filename " matches: " (first (sort-by second > matches)))
      (println "No match for: " filename))))

(defn search-for-duplicates [dir]
  (->> (filter #(.isFile %) (file-seq (io/file dir)))
       (map match-and-index-file)))
