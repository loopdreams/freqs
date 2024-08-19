(ns freqs.events
  (:require
   [re-frame.core :as re-frame]
   [freqs.db :as db]
   [freqs.frequencies :as f]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))


(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::update-text-input
 (fn [db [_ text]]
   (assoc db :text-input text)))

(re-frame/reg-event-db
 ::get-word-frequencies
 (fn [db [_ case?]]
   (let [text-input (:text-input db)
         results (if case? (f/count-word-frequencies text-input true)
                     (f/count-word-frequencies text-input) )]
     (assoc db :results results))))

(re-frame/reg-event-db
 ::update-stopwords-pref
 (fn [db [_ status]]
   (assoc db :stopwords? status)))
