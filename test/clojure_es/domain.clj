(ns clojure-es.domain)

(defmulti handle (fn [account event] (:type event)))

(defmethod handle :account-created [account event]
  {:balance 0})

(defmethod handle :owner-changed [account event]
  (assoc account :owner (:owner event)))

(defmethod handle :deposit-performed [account event]
  (update account :balance (fn [balance] (+ balance (:amount event)))))

(defmethod handle :withdrawal-performed [account event]
  (update account :balance (fn [balance] (- balance (:amount event)))))

(defmethod handle :default [account event] account)

(def reducer {:init {} :handle handle})
