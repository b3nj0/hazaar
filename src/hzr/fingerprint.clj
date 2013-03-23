(ns hzr.fingerprint
  (:import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D))

;; fast fourier transform

(defn- fft [bytes]
  "Apply a fast-fourier-transform to a byte array of PCM audio data to convert
   the time series data to frequency data."
  (let [bc (alength bytes)
        a (double-array bytes)
        fft (DoubleFFT_1D. bc)]
    (.realForward fft a)
    a))

;; hash frequency data

(defn- magnitude [n]
  (Math/log (+ 1 (Math/abs n))))

(defn- find-max-mag [#^doubles xs from to]
  "Find the frequency with the highest magnitude in a given range."
  (loop [xs xs
         i from
         ret [0 0.0]]
    (if (> i to)
      ret
      (recur xs (inc i) (if (> (magnitude (aget xs i)) (get ret 1)) [i (magnitude (aget xs i))] ret)))))

(defn- hash-audio [freq-data]
  "Hash the audio data by finding the frequencies with the highest magnitude in
   the ranges 40-80hz, 80-120hz, 121-180hz and 180-300hz. Combine the selected
   frequencies into a single integer which can be used as a fingerprint for that
   part of the audio stream."
  (let [[[m40 _] [m80 _] [m120 _] [m180 _] m300] (map #(find-max-mag freq-data (get % 0) (get % 1)) [[0 40] [41 80] [81 120] [121 180] [181 300]])
        fuzzed (fn [x] (- x (mod x 2)))] ;; fuzz for error dampening
    (+ (* 10000000 (fuzzed m180))
       (* 10000 (fuzzed m120))
       (* 100 (fuzzed m80))
       (fuzzed m40))))

;; fingerprint

(defn fingerprint [bytes]
  "Generate a fingerprint for a byte array of PCM encoded audio data."
  (hash-audio (fft bytes)))
