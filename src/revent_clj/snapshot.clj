(ns revent-clj.snapshot
  (:require [revent-clj.reducer :refer :all]
            [revent-clj.either :refer :all]
            [revent-clj.version :as version]))

(defrecord Snapshot [aggregate version timestamp])

(defn create-reducer [reducer]
  (->Reducer
    (->Snapshot (:init reducer) 0 nil)
    (fn [snapshot event]
      (->Snapshot
        ((:handle reducer) (:aggregate snapshot) (:payload event))
        (:version event)
        (:timestamp event)))))

(defn validate [snapshot expected-version]
  (if (version/validate expected-version (:version snapshot))
    (success snapshot)
    (failure :invalid-aggregate-version)))
