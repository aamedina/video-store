(ns video-store.customer
  (:require [video-store.movie :as movie]
            [video-store.rental :as rental]
            [hiccup.core :as html]
            [hiccup.def :refer [defelem]]))

(defn create [name]
  {:type :customer :name name})

(defn add-rental [customer rental]
  (update-in customer [:rentals] (fnil conj []) rental))
