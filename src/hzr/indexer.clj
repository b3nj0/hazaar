(ns hzr.indexer
  (:require [clojure.java.io :as io]
            [hzr.audio :as audio]
            [hzr.fingerprint :as fingerprint])
  (:import [java.util Arrays]))

(defn index-audio-file [file]
  (audio/decode-audio-file file
                           (fn [in]   (let [chunk-size 4096
                                           buffer (make-array Byte/TYPE chunk-size)]
                                       (loop [bc (.read in buffer)]
                                         (if-not (= bc -1)
                                           (do
                                             (println (fingerprint/fingerprint buffer))
                                             (recur (.read in buffer)))))))))

(defn index [dir]
  (->> (filter #(.isFile %) (file-seq (io/file dir)))
       (map index-audio-file)))
