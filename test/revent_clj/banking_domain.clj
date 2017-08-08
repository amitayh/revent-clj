(ns revent-clj.banking-domain
  (:require [revent-clj.core :refer :all]
            [revent-clj.either :refer :all]))

; --- Example domain for tests ---

; --- Events ---

(defrecord AccountCreated [])

(defrecord OwnerChanged [owner])

(defrecord DepositPerformed [amount])

(defrecord WithdrawalPerformed [amount])

; --- Commands ---

(defn- can-withdraw? [account amount]
  (>= (:balance account) amount))

(defn create-account [owner initial-balance]
  (fn [account]
    (success [(->AccountCreated)
              (->OwnerChanged owner)
              (->DepositPerformed initial-balance)])))

(defn deposit [amount]
  (fn [account]
    (success [(->DepositPerformed amount)])))

(defn withdraw [amount]
  (fn [account]
    (if (can-withdraw? account amount)
      (success [(->WithdrawalPerformed amount)])
      (failure :insufficient-funds))))

; -- Event handling ---

(defmulti handle (fn [account event] (class event)))

(defmethod handle AccountCreated [account event]
  {:balance 0})

(defmethod handle OwnerChanged [account event]
  (assoc account :owner (:owner event)))

(defmethod handle DepositPerformed [account event]
  (update account :balance (partial + (:amount event))))

(defmethod handle WithdrawalPerformed [account event]
  (update account :balance (fn [balance] (- balance (:amount event)))))

(defmethod handle :default [account event] account)

; -- Reducer ---

(def reducer (->Reducer {} handle))
