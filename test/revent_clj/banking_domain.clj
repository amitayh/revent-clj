(ns revent-clj.banking-domain
  (:require [revent-clj.either :refer :all]))

; --- Example domain for tests ---

; --- Commands ---

(defn- can-withdraw? [account amount]
  (>= (:balance account) amount))

(defn create-account [owner initial-balance]
  (fn [account]
    (success [{:type :account-created}
              {:type :owner-changed :owner owner}
              {:type :deposit-performed :amount initial-balance}])))

(defn deposit [amount]
  (fn [account]
    (success [{:type :deposit-performed :amount amount}])))

(defn withdraw [amount]
  (fn [account]
    (if (can-withdraw? account amount)
      (success [{:type :withdrawal-performed :amount amount}])
      (failure :insufficient-funds))))

; -- Event handling ---

(defmulti handle (fn [account event] (:type event)))

(defmethod handle :account-created [account event]
  {:balance 0})

(defmethod handle :owner-changed [account event]
  (assoc account :owner (:owner event)))

(defmethod handle :deposit-performed [account event]
  (update account :balance (partial + (:amount event))))

(defmethod handle :withdrawal-performed [account event]
  (update account :balance (fn [balance] (- balance (:amount event)))))

(defmethod handle :default [account event] account)

(def reducer {:init {} :handle handle})
