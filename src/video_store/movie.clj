(ns video-store.movie)

(defn create [title price-code]
  {:type :movie
   :title title
   :price-code price-code})
