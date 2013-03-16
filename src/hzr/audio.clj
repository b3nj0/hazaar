(ns hzr.audio
  (:use [clojure.java.io :only [file]])
  (:import [javax.sound.sampled AudioSystem AudioInputStream AudioFormat AudioFormat$Encoding]
           [java.io File]))

(defn- base-to-decoded-format [base-format]
  (let [big-endian false]
    (AudioFormat. AudioFormat$Encoding/PCM_SIGNED
                  (.getSampleRate base-format)
                  16
                  (.getChannels base-format)
                  (* 2 (.getChannels base-format))
                  (.getSampleRate base-format)
                  big-endian)))

(defn decode [filename fn]
  (with-open [in (AudioSystem/getAudioInputStream (file filename))
              decoded-in (AudioSystem/getAudioInputStream (base-to-decoded-format (.getFormat in)) in)]
    (fn decoded-in)))
