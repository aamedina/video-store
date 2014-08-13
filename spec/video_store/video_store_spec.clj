(ns video-store.video-store-spec
  (:require [video-store.customer :as customer]
            [video-store.statements :as stmt]
            [video-store.movie :as movie]
            [video-store.rental :as rental]
            [speclj.core :refer :all]))

;; - duplication (maybe solved by fixtures / before blocks)

;; - testing user interface / presentation / strings
;;   - could test values instead
;;   - make it more obvious what the underlying failures are
;;   - UI evolves independently (fragile tests)
;;   - i18n
;;   - testing through the UI doesn't discourage UI+logic coupling

;; extrapolating the UI+logic issue:
;;   - UI tests can be flaky

(describe "video store"
  (it "produces a statement for a single new release"
    (let [customer (-> (customer/create "Fred")
                       (customer/add-rental
                         (rental/create
                           (movie/create "The Cell" ::rental/new-release)
                           3)))]
      (should= customer
               {:rentals
                [{:type :rental
                  :movie {:type :movie
                          :title "The Cell"
                          :price-code :video-store.rental/new-release}
                  :days 3}]
                :type :customer
                :name "Fred"})))

  (it "produces a statement for two new releases"
    (let [customer (-> (customer/create "Fred")
                       (customer/add-rental
                         (rental/create
                           (movie/create "The Cell"
                                         ::rental/new-release)
                           3))
                       (customer/add-rental
                         (rental/create
                           (movie/create "The Tigger Movie"
                                         ::rental/new-release)
                           3)))]
      (should= customer
               {:rentals
                [{:type :rental
                  :movie {:type :movie
                          :title "The Cell"
                          :price-code :video-store.rental/new-release},
                  :days 3}
                 {:type :rental
                  :movie {:type :movie
                          :title "The Tigger Movie"
                          :price-code :video-store.rental/new-release}
                  :days 3}]
                :type :customer
                :name "Fred"})))

  (it "produces a statement for a single children's movie"
    (let [customer (-> (customer/create "Fred")
                       (customer/add-rental
                         (rental/create
                           (movie/create "The Tigger Movie"
                                         ::rental/childrens)
                           3)))]
      (should= customer
               {:rentals
                [{:days 3
                  :movie {:type :movie
                          :title "The Tigger Movie"
                          :price-code :video-store.rental/childrens}
                  :type :rental}]
                :name "Fred"
                :type :customer})))

  (it "produces a statement for multiple regular movies"
    (let [customer (-> (customer/create "Fred")
                       (customer/add-rental
                         (rental/create
                           (movie/create "Plan 9 from Outer Space"
                                         ::rental/regular)
                           1))
                       (customer/add-rental
                         (rental/create
                           (movie/create "8 1/2" ::rental/regular)
                           2))
                       (customer/add-rental
                         (rental/create
                           (movie/create "Eraserhead" ::rental/regular)
                           3)))]
      (should= customer
               {:rentals [{:type :rental
                           :movie {:type :movie
                                   :title "Plan 9 from Outer Space"
                                   :price-code :video-store.rental/regular}
                           :days 1}
                          {:type :rental
                           :movie {:type :movie
                                   :title "8 1/2"
                                   :price-code :video-store.rental/regular}
                           :days 2}
                          {:type :rental
                           :movie {:type :movie
                                   :title "Eraserhead"
                                   :price-code :video-store.rental/regular}
                           :days 3}]
                :type :customer, :name "Fred"}))))

