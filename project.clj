(defproject hazaar "0.1.0-SNAPSHOT"
  :description "Music matching service"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.googlecode.soundlibs/mp3spi "1.9.5-1"]
                 [com.github.rwl/jtransforms "2.4.0"]]
  :warn-on-reflection true
  :main hzr.main)
