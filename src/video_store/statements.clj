(ns video-store.statements
  (:require [video-store.rental :as rental :refer [calculate-price]]
            [video-store.customer :as customer]
            [video-store.movie :as movie]
            [hiccup.core :refer [html]]))

(defmulti statement (fn [customer render-as] render-as)
  :default :text)

(defn statement-body [customer]
  (reduce (fn [acc rental]
            (let [this-amount (calculate-price rental)
                  line-item (str "\t" (:title (:movie rental))
                                 "\t" (double this-amount) "\n")
                  added-points (if (and (< 1 (:days rental))
                                        (= ::rental/new-release
                                           (:price-code (:movie rental))))
                                 2
                                 1)]
              (-> acc
                  (update-in [:body-text] str line-item)
                  (update-in [:total-amount] + this-amount)
                  (update-in [:frequent-renter-points] + added-points))))
          {:body-text "" :total-amount 0.0 :frequent-renter-points 0}
          (:rentals customer)))

(defn statement-header [customer]
  (format "Rental Record for %s\n" (:name customer)))

(defn statement-footer [amount points]
  (format "You owed %.1f\nYou earned %d frequent renter points\n"
          amount
          points))

(defmethod statement :html
  [customer _]
  (let [{:keys [body-text total-amount frequent-renter-points]}
        (statement-body customer)]
    (html
     [:div.statement
      [:h1 (statement-header customer)]
      [:p body-text]
      [:p.footer (statement-footer total-amount frequent-renter-points)]])))

(defmethod statement :text
  [customer _]
  (let [{:keys [body-text total-amount frequent-renter-points]}
        (statement-body customer)]
    (str (statement-header customer)
         body-text
         (statement-footer total-amount frequent-renter-points))))
