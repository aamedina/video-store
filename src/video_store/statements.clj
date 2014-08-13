(ns video-store.statements
  (:require [video-store.rental :as rental :refer [calculate-price]]
            [video-store.customer :as customer]
            [video-store.movie :as movie]
            [hiccup.core :refer [html]]))

(defmulti statement (fn [customer render-as] render-as)
  :default :text)

(defmulti statement-header (fn [customer render-as] render-as)
  :default :text)

(defmulti statement-footer (fn [statement render-as] render-as)
  :default :text)

(defmulti rental-line-item (fn [rental this-amount render-as] render-as)
  :default :text)

(defmethod statement-header :text
  [customer _]
  (format "Rental Record for %s\n" (:name customer)))

(defmethod statement-footer :text
  [{:keys [this-amount frequent-renter-points]} _]
  (format "You owed %.1f\nYou earned %d frequent renter points\n"
          this-amount frequent-renter-points))

(defmethod statement-header :html
  [customer _]
  [:div.header
   [:h1 (str "Rental Record for " (:name customer))]])

(defmethod rental-line-item :text
  [rental this-amount _]
  (str "\t" (:title (:movie rental))
       "\t" (double this-amount) "\n"))

(defmethod rental-line-item :html
  [rental this-amount _]
  (str " " (:title (:movie rental)) (double this-amount) " "))

(defn statement-body
  [customer render-as]
  (reduce (fn [acc rental]
            (let [this-amount (calculate-price rental)
                  line-item (rental-line-item rental this-amount render-as)
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

(defmethod statement-footer :html
  [{:keys [total-amount frequent-renter-points]} _]
  [:div.footer
   [:h2 (str "You owed " total-amount )]
   [:h2 (str "You earned " frequent-renter-points " frequent renter points")]])

(defmethod statement :html
  [customer _]
  (let [{:keys [body-text total-amount frequent-renter-points] :as stmt}
        (statement-body customer :html)]
    (html
     [:div.statement
      (statement-header customer :html)      
      [:div.body
       [:p body-text]]
      (statement-footer customer :html)      
      (statement-footer stmt :html)])))

(defmethod statement :text
  [customer _]
  (let [{:keys [body-text total-amount frequent-renter-points] :as statement}
        (statement-body customer :text)]
    (str (statement-header customer :text)
         body-text
         (statement-footer statement :text))))
