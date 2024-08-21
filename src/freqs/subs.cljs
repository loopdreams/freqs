(ns freqs.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::results
 (fn [db]
   (:results db)))

(re-frame/reg-sub
 ::text-input
 (fn [db]
   (:text-input db)))

(re-frame/reg-sub
 ::result-filters
 (fn [db [_ filter]]
   (get-in db [:result-filters filter])))
