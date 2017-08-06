(ns clojure-es.cqrs
  (:require [clojure-es.either :refer :all]))

(defn command
  ([aggregate-id to-events]
   (command aggregate-id to-events nil))

  ([aggregate-id to-events expected-version]
   {:aggregate-id     aggregate-id
    :to-events        to-events
    :expected-version expected-version}))

(defn handle [load-snapshot persist-events command]
  (let-err
    [snapshot (load-snapshot (:aggregate-id command) (:expected-version command))
     command-events ((:to-events command) (:aggregate snapshot))
     persisted-events (persist-events (:aggregate-id command) command-events (:version snapshot))]
    persisted-events))
