(ns hzr.audio
  (:require [clojure.java.io :as io])
  (:import [javax.sound.sampled AudioSystem AudioInputStream AudioFormat AudioFormat$Encoding DataLine$Info TargetDataLine]
           [java.io File]))

;; create an input stream that wraps an audio input stream converting
;; the stream from stereo to mono by averaging the samples on the left
;; and right channels

(gen-class
   :name hzr.audio.StereoToMonoInputStream
   :extends java.io.InputStream
   :init init
   :state state
   :prefix "s2m"
   :constructors {[InputStream] []})

(defn s2m-init [in]
  [] {:in in :next (atom -1)})

(defn s2m-read-void [this]
  (let [s (.state this)
        next @(:next s)]
    (if (= -1 next)
      (let [b0 (.read (:in s))
            b1 (.read (:in s))
            b2 (.read (:in s))
            b3 (.read (:in s))
            left  (+ (* b0 256) b1)
            right (+ (* b2 256) b3)
            mono (/ (+ left right) 2)]
        (reset! (:next s) (quot mono 256))
        (mod mono 256))
      (do (reset! (:next s) -1)
          next))))

(defn stereo->mono [in]
  (hzr.audio.StereoToMonoInputStream. in))

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
