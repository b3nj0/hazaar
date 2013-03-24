(ns hzr.audio
  (:require [clojure.java.io :as io])
  (:import [javax.sound.sampled AudioSystem AudioInputStream AudioFormat AudioFormat$Encoding]))

(defn- base-to-decoded-format [base-format]
  (let [sample-size-in-bits 16
        num-channels (.getChannels base-format)
        num-bytes-in-each-frame (* (/ sample-size-in-bits 8) num-channels)
        big-endian true]
    (AudioFormat. AudioFormat$Encoding/PCM_SIGNED
                  (.getSampleRate base-format)
                  sample-size-in-bits
                  num-channels
                  num-bytes-in-each-frame
                  (.getFrameRate base-format)
                  big-endian)))

(defn decode-audio-file [filename fn]
  (with-open [in (AudioSystem/getAudioInputStream (io/file filename))
              stereo-in (AudioSystem/getAudioInputStream (base-to-decoded-format (.getFormat in)) in)]
    (fn stereo-in)))

(defn decoded-audio-file [filename]
  (let [buffer (java.io.ByteArrayOutputStream.)]
    (decode-audio-file filename #(io/copy % buffer))
    (.toByteArray buffer)))
