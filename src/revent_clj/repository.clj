(ns revent-clj.repository
  (:require [revent-clj.reducer :as reducer]
            [revent-clj.snapshot :as snapshot]))

(defn load-snapshot
  ([read-events reducer aggregate-id]
   (load-snapshot read-events reducer aggregate-id nil))

  ([read-events reducer aggregate-id expected-version]
   (let [snapshot-reducer (snapshot/create-reducer reducer)
         events (read-events aggregate-id)
         snapshot (reducer/reduce-events snapshot-reducer events)]
     (snapshot/validate snapshot expected-version))))
