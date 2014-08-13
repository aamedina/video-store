(ns video-store.rental
  (:require [video-store.movie :as movie]))

(defn create [movie days]
  {:type :rental
   :movie movie
   :days days})

(defmulti calculate-price (comp :price-code :movie) :default ::regular)

(defmethod calculate-price ::regular
  [rental]
  (+ 2 (if (< 2 (:days rental))
         (* 1.5 (- (:days rental) 2))
         0)))

(defmethod calculate-price ::new-release
  [rental]
  (* 3 (:days rental)))

(defmethod calculate-price ::childrens
  [rental]
  (+ 1.5 (if (< 3 (:days rental))
           (* 1.5 (- (:days rental) 3))
           0)))


