(ns org.amitayh.revent-clj.cqrs
  (:require [org.amitayh.either :refer :all]))

(defrecord Command [aggregate-id to-events expected-version])

(defn handle [load-snapshot persist-events command]
  (chain
    [snapshot (load-snapshot (:aggregate-id command) (:expected-version command))
     command-events ((:to-events command) (:aggregate snapshot))
     persisted-events (persist-events (:aggregate-id command) command-events (:version snapshot))]
    persisted-events))
