(ns revent-clj.memory-event-store
  (:require [revent-clj.either :refer :all]
            [revent-clj.version :as version]))

(defn- to-event-stream [payloads last-version timestamp]
  (let [next-version (inc last-version)
        next-versions (iterate inc next-version)
        combiner (fn [payload version]
                   {:version   version
                    :payload   payload
                    :timestamp timestamp})]
    (mapv combiner payloads next-versions)))

(defn- last-version-for [store stream-id expected-version]
  (let [stream (get store stream-id [])
        last-version (or (-> stream (last) (:version)) 0)]
    (if (version/validate expected-version last-version)
      (success last-version)
      (failure :concurrent-modification))))

(defn- append-events [new-events]
  (fn [old-events] (vec (concat old-events new-events))))

; --- Public ---

(defn now [] (java.time.Instant/now))

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
         (let [new-events (to-event-stream events last-version (now))
               new-store (update current-store stream-id (append-events new-events))]
           (if (compare-and-set! store current-store new-store)
             (success new-events)
             (failure :concurrent-modification))))))))

(defn read-events [store stream-id]
  (get @store stream-id []))
