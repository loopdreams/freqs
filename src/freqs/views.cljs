(ns freqs.views
  (:require
   [re-frame.core :as re-frame]
   [freqs.subs :as subs]
   [freqs.events :as events]
   [freqs.frequencies :as f]))

(defn input-form []
  (let [text-input @(re-frame/subscribe [::subs/text-input])]
    [:div
     [:div
      [:textarea {:rows 5
                  :cols 80
                  :placeholder "Enter Text"
                  :on-change #(re-frame/dispatch [::events/update-text-input (-> % .-target .-value)])}]]
     [:div
      [:button {:on-click #(re-frame/dispatch [::events/get-word-frequencies])}
       "Get word frequencies"]]]))

(defn results-filters []
  [:div
   [:label "Include stopwords?"
    [:input {:type "checkbox"
             :on-change #(re-frame/dispatch [::events/update-stopwords-pref (-> % .-target .-checked)])}]]])


(defn results-display [results]
  (let [stopwords? @(re-frame/subscribe [::subs/stopwords?])
        results (if stopwords?
                  results
                  (f/remove-stopwords results))]
    [:div
     [results-filters]
     [:div
      [:h2 "Stats"]
      [:ul
       [:li (str "Number of Words: " (f/word-count results))]
       [:li (str "Number of Unique Words: " (f/unique-wrods results))]
       [:li "Longest Words: "
        (into [:ul]
              (for [w (f/longest-words results 5)
                    :let [[word length] w]]
                [:li (str word " - " length)]))]]]
     [:div
      [:h2 "Word Frequencies"]
      (into [:ol]
            (for [item (reverse (sort-by val results))
                  :let [[word fq] item]]
              [:li (str word " - " fq)]))]]))

;; some options to include
;; - ignore case (true/false)
;; - ignore stopwords (true/false)
;;
;; Stats to display
;; - total words
;; - total unique words
;; - longest word


(defn main-panel []
  (let [results @(re-frame/subscribe [::subs/results])]
    [:div
     [:h1 "Word Frequency Calculator"]
     [input-form]
     (when results
       [results-display results])]))
