(ns hzr.fingerprint
  (:import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D))

(defn- magnitude [n]
  (Math/log (+ 1 (Math/abs n))))

(defn- find-max-mag [#^doubles xs from to]
  (loop [xs xs
         i from
         ret [0 0.0]]
    (if (> i to)
      ret
      (recur xs (inc i) (if (> (magnitude (aget xs i)) (get ret 1)) [i (magnitude (aget xs i))] ret)))))


(defn- hash [freq-data]
  (let [[[m40 _] [m80 _] [m120 _] [m180 _] m300] (map #(find-max-mag freq-data (get % 0) (get % 1)) [[0 40] [41 80] [81 120] [121 180] [181 300]])
        fuzzed (fn [x] (- x (mod x 2)))] ;; fuzz for error dampening
    (+ (* 10000000 (fuzzed m180))
       (* 10000 (fuzzed m120))
       (* 100 (fuzzed m80))
       (fuzzed m40)) ))

(defn- fft [bytes]
  "convert the array of bytes into a fingerprints"
  (let [bc (alength bytes)
        a (double-array bytes)
        fft (DoubleFFT_1D. bc)]
    (.realForward fft a)
    a))

(defn fingerprint [bytes]
  (hash (fft bytes)))