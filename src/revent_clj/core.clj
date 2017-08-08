(ns revent-clj.core)

(defrecord Event [version payload timestamp])

(defrecord Reducer [init handle])

(defrecord Snapshot [aggregate version timestamp])
