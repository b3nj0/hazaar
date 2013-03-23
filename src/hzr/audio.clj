(ns hzr.audio
  (:require [clojure.java.io :as io])
  (:import [javax.sound.sampled AudioSystem AudioInputStream AudioFormat AudioFormat$Encoding DataLine$Info TargetDataLine]
           [java.io File InputStream]))

(defn- base-to-decoded-format [base-format]
  (let [big-endian true]
    (AudioFormat. AudioFormat$Encoding/PCM_SIGNED
                  (.getSampleRate base-format)
                  16
                  (.getChannels base-format)
                  (* 2 (.getChannels base-format))
                  (.getSampleRate base-format)
                  big-endian)))

(defn decode-audio-file [filename fn]
  (with-open [in (AudioSystem/getAudioInputStream (io/file filename))
              stereo-in (AudioSystem/getAudioInputStream (base-to-decoded-format (.getFormat in)) in)
              mono-in (stereo->mono stereo-in)]
    (fn mono-in)))

(defn decoded-audio-file [filename]
  (let [buffer (java.io.ByteArrayOutputStream.)]
    (decode-audio-file filename #(io/copy % buffer))
    (.toByteArray buffer)))

(defn- microphone-format []
  (let [sample-rate 44100
        sample-size-in-bits 8
        channels 1
        signed true
        big-endian true]
    (AudioFormat. sample-rate sample-size-in-bits channels signed big-endian)))

(defn record-microphone [fn]
  (let [info (DataLine$Info. (type TargetDataLine) (microphone-format))]
    (with-open [line (AudioSystem/getLine info)]
      (.start line)
      (fn line)
      (.stop line))))
