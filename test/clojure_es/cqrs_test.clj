(ns clojure-es.cqrs-test
  (:require [clojure.test :refer :all]
            [clojure-es.either :refer :all]
            [clojure-es.cqrs :as cqrs]
            [clojure-es.memory-event-store :as s]
            [clojure-es.banking-domain :as d]
            [clojure-es.repository :as r]))

(def account-id 1)

(def create-command (d/create-account "John Doe" 10))

(def deposit-command (d/deposit 10))

(def ^:dynamic handle)

(def ^:dynamic load-snapshot)

(defn load-account []
  (-> account-id
      (load-snapshot)
      (first)
      (:aggregate)))

(defn last-version [[events error]]
  (if (nil? error)
    (-> events (last) (:version))
    nil))

(defn setup-handler [test]
  (let [store (s/empty-store)
        read-events (partial s/read-events store)
        persist-events (partial s/persist-events store s/now)]
    (binding [load-snapshot (partial r/load-snapshot read-events d/reducer)
              handle (fn [command] (cqrs/handle load-snapshot persist-events command))]
      (test))))

(use-fixtures :each setup-handler)

(deftest command-handling-without-events
  (handle (cqrs/command account-id create-command))
  (is (= {:owner "John Doe" :balance 10}
         (load-account))))

(deftest handle-multiple-commands
  (handle (cqrs/command account-id create-command))
  (handle (cqrs/command account-id deposit-command))
  (is (= {:owner "John Doe" :balance 20}
         (load-account))))

(deftest fail-if-expected-version-is-wrong
  (let [result (handle (cqrs/command account-id create-command))
        version (last-version result)
        wrong-expected-version (dec version)]
    (is (= (handle (cqrs/command account-id deposit-command wrong-expected-version))
           (failure :invalid-aggregate-version)))))

(deftest succeed-if-expected-version-is-correct
  (let [result (handle (cqrs/command account-id create-command))
        version (last-version result)]
    (handle (cqrs/command account-id deposit-command version))
    (is (= {:owner "John Doe" :balance 20}
           (load-account)))))

(deftest fail-if-command-is-rejected
  (handle (cqrs/command account-id create-command))
  (is (= (handle (cqrs/command account-id (d/withdraw 20)))
         (failure :insufficient-funds))))
