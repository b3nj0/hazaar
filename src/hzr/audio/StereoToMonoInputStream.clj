(ns hzr.audio.StereoToMonoInputStream
  (:gen-class
   :extends java.io.InputStream
   :constructors {[java.io.InputStream] []}
   :init init
   :state state
   :prefix "s2m-"))

;; create an input stream that wraps an audio input stream converting
;; the stream from stereo to mono by averaging the samples on the left
;; and right channels

(defn s2m-init [^java.io.InputStream in]
  [[] {:in in :next (atom -1)}])

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
