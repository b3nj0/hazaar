(ns hzr.indexer
  (:require [clojure.java.io :as io]
            [hzr.audio :as audio]
            [hzr.fingerprint :as fingerprint])
  (:import [java.util Arrays]))

(defn index-audio-file [file]
  (let [bytes (audio/decoded-audio-file file)
        byte-count (alength bytes)
        chunk-size 4096]
    (loop [pos 0]
      (if-not (> pos byte-count)
        (do
          (println (fingerprint/fingerprint (Arrays/copyOfRange bytes pos (+ pos chunk-size))))
          (recur (+ pos chunk-size)))))))

(defn index [dir]
  (->> (filter #(.isFile %) (file-seq (io/file dir)))
       (map index-audio-file)))
