(ns org.amitayh.revent-clj.memory-event-store
  (:import (java.time Instant))
  (:require [org.amitayh.revent-clj.either :refer :all]
            [org.amitayh.revent-clj.version :as version]))

(defrecord Event [stream-id version payload timestamp])

(defn- to-event-stream [stream-id payloads last-version timestamp]
  (let [next-version (inc last-version)
        next-versions (iterate inc next-version)
        combiner (fn [payload version] (->Event stream-id version payload timestamp))]
    (mapv combiner payloads next-versions)))

(defn- last-version-for [store stream-id expected-version]
  (let [stream (get store stream-id [])
        last-version (or (-> stream last :version) 0)]
    (if (version/validate expected-version last-version)
      (success last-version)
      (failure :concurrent-modification))))

(defn- append-events [new-events]
  (fn [old-events] (vec (concat old-events new-events))))

; --- Public ---

(defn now [] (Instant/now))

(defn empty-store [] (atom {}))

; This implementation doesn't support appending
; events to multiple streams concurrently
(defn persist-events
  ([store now stream-id events]
   (persist-events store now stream-id events nil))

  ([store now stream-id events expected-version]
   (let [current-store @store]
     (bind
       (last-version-for current-store stream-id expected-version)
       (fn [last-version]
         (let [new-events (to-event-stream stream-id events last-version (now))
               new-store (update current-store stream-id (append-events new-events))]
           (if (compare-and-set! store current-store new-store)
             (success new-events)
             (failure :concurrent-modification))))))))

(defn read-events [store stream-id from-version max-count]
  (->> (get @store stream-id [])
       (drop (dec from-version))
       (take max-count)))

