(ns clojure-es.version)

(defn validate [expected actual]
  (or (nil? expected)
      (= expected actual)))
