(ns hzr.fingerprint
  (:import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D))

(defn- magnitude [n]
  (Math/log (+ 1 (Math/abs n))))

(defn- find-max-magnitude-in-range [#^doubles xs]
  (areduce xs i ret [0 0.0] (if (> (magnitude (aget xs i)) (get ret 1)) [i (magnitude (aget xs i))] ret)))

(defn- hash [freq-data]
  true)

(defn- fft [bytes]
  "convert the array of bytes into a fingerprints"
  (let [bc (alength bytes)
        a (double-array bytes)
        fft (DoubleFFT_1D. bc)]
    (.realForward fft a)
    a))
