(ns org.amitayh.revent-clj.snapshot
  (:require [org.amitayh.either :refer :all]
            [org.amitayh.revent-clj.reducer :refer :all]
            [org.amitayh.revent-clj.version :as version]))

(defrecord Snapshot [aggregate version timestamp])

(defn create-reducer [reducer]
  (->Reducer
    (->Snapshot (:init reducer) nil nil)
    (fn [snapshot event]
      (->Snapshot
        ((:handle reducer) (:aggregate snapshot) (:payload event))
        (:version event)
        (:timestamp event)))))

(defn validate [snapshot expected-version]
  (if (version/validate expected-version (:version snapshot))
    (success snapshot)
    (failure :invalid-aggregate-version)))

(defn next-version [snapshot]
  (inc (or (:version snapshot) 0)))
