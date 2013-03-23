(ns hzr.main
  (:require [hzr.indexer :as indexer]))

(defn -main [dir]
  (indexer/search-for-duplicates dir))
