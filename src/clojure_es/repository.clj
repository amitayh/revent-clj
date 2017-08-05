(ns clojure-es.repository
  (:require [clojure-es.either :refer :all]
            [clojure-es.version :as version]))

(defn- create-snapshot-reducer [reducer]
  {:init   {:aggregate (:init reducer) :version 0}
   :handle (fn [snapshot event]
             {:aggregate ((:handle reducer) (:aggregate snapshot) (:payload event))
              :version   (:version event)
              :timestamp (:timestamp event)})})

(defn- reduce-events [reducer events]
  (reduce (:handle reducer) (:init reducer) events))

(defn- validate-snapshot [snapshot expected-version]
  (if (version/validate expected-version (:version snapshot))
    (success snapshot)
    (failure :invalid-aggregate-version)))

(defn load-snapshot
  ([read-events reducer aggregate-id]
   (load-snapshot read-events reducer aggregate-id nil))

  ([read-events reducer aggregate-id expected-version]
   (let [snapshot-reducer (create-snapshot-reducer reducer)
         events (read-events aggregate-id)
         snapshot (reduce-events snapshot-reducer events)]
     (validate-snapshot snapshot expected-version))))
