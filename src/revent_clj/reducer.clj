(ns revent-clj.reducer)

(defrecord Reducer [init handle])

(defn reduce-events [reducer events]
  (reduce (:handle reducer) (:init reducer) events))
