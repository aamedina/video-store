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
      (should= (str "Rental Record for Fred\n"
                    "\tThe Cell\t9.0\n"
                    "You owed 9.0\n"
                    "You earned 2 frequent renter points\n")
               (stmt/statement customer :text))))

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
      (should= (str "Rental Record for Fred\n"
                    "\tThe Cell\t9.0\n"
                    "\tThe Tigger Movie\t9.0\n"
                    "You owed 18.0\n"
                    "You earned 4 frequent renter points\n")
               (stmt/statement customer :text))))

  (it "produces a statement for a single children's movie"
    (let [customer (-> (customer/create "Fred")
                       (customer/add-rental
                         (rental/create
                           (movie/create "The Tigger Movie"
                                         ::rental/childrens)
                           3)))]
      (should= (str "Rental Record for Fred\n"
                    "\tThe Tigger Movie\t1.5\n"
                    "You owed 1.5\n"
                    "You earned 1 frequent renter points\n")
               (stmt/statement customer :text))))

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
      (should= (str "Rental Record for Fred\n"
                    "\tPlan 9 from Outer Space\t2.0\n"
                    "\t8 1/2\t2.0\n"
                    "\tEraserhead\t3.5\n"
                    "You owed 7.5\n"
                    "You earned 3 frequent renter points\n")
               (stmt/statement customer :text)))))

