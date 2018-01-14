(ns org.amitayh.revent-clj.repository
  (:require [org.amitayh.revent-clj.either :refer :all]
            [org.amitayh.revent-clj.reducer :as reducer]
            [org.amitayh.revent-clj.snapshot :as snapshot]))

(defn- no-more-events? [events page-size]
  (< (count events) page-size))

(defn load-snapshot
  ([read-events reducer page-size aggregate-id expected-version]
   (let [snapshot (load-snapshot read-events reducer page-size aggregate-id)]
     (bind snapshot #(snapshot/validate % expected-version))))

  ([read-events reducer page-size aggregate-id]
   (let [snapshot-reducer (snapshot/create-reducer reducer)
         {init-snapshot :init handle :handle} snapshot-reducer]
     (loop [snapshot init-snapshot]
       (let [next-version (inc (:version snapshot))
             events (read-events aggregate-id next-version page-size)
             updated-snapshot (reduce handle snapshot events)]
         (if (no-more-events? events page-size)
           (success updated-snapshot)
           (recur updated-snapshot)))))))
