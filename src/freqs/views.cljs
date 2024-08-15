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
      [:textarea {:class "block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                  :rows 5
                  :cols 80
                  :placeholder "Paste Your Text Here"
                  :on-change #(re-frame/dispatch [::events/update-text-input (-> % .-target .-value)])}]]
     [:div
      [:button {:class "mt-6 select-none rounded-lg bg-blue-500 py-3 px-6 text-center align-middle font-sans text-xs font-bold uppercase text-white shadow-md shadow-blue-500/20 transition-all hover:shadow-lg hover:shadow-blue-500/40 focus:opacity-[0.85] focus:shadow-none active:opacity-[0.85] active:shadow-none disabled:pointer-events-none disabled:opacity-50 disabled:shadow-none"
                :on-click #(re-frame/dispatch [::events/get-word-frequencies])}
       "Get word frequencies"]]]))

(defn results-filters []
  [:div
   [:input {:type "checkbox"
            :class "w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600"
            :on-change #(re-frame/dispatch [::events/update-stopwords-pref (-> % .-target .-checked)])}]
   [:label {:class "ms-2 text-sm font-medium text-gray-900 dark:text-gray-300"} "Include stopwords?"]])

(defn stat-component [title value]
  [:div {:class "flex justify-between w-64 py-2"}
   [:p {:class "font-bold pr-5"} title]
   [:p {:class "text-fuchsia-700"} value]])

(defn words-component [word count]
  [:div {:class "flex justify-between w-48 py-2"}
   [:p word]
   [:p count]])


(defn results-display [results]
  (let [stopwords? @(re-frame/subscribe [::subs/stopwords?])
        results (if stopwords?
                  results
                  (f/remove-stopwords results))]
    [:div
     [results-filters]
     [:hr {:class "my-6"}]
     [:div {:class "pt-4"}
      [:h2 {:class "text-2xl font-bold py-2"} "Stats"]
      [stat-component "Number of Words" (f/word-count results)]
      [stat-component "Number of Unique Words" (f/unique-wrods results)]
      [:p {:class "font-bold pr-5 py-2"} "Longest Words:"]
      (into
       [:div {:class "flex py-2"}]
       (for [w (f/longest-words results 5)
             :let [[word length] w]]
         [:p {:class "bg-blue-100 text-blue-800 text-sm font-medium me-2 px-2.5 py-0.5 rounded dark:bg-blue-900 dark:text-blue-300"}
          (str word " (" length ")")]))]
     [:hr {:class "my-6"}]

     [:div
      [:h2 {:class "text-2xl font-bold pb-4"} "Word Frequencies"]
      (into [:div]
            (for [item (reverse (sort-by val results))
                  :let [[word fq] item]]
              [words-component word fq]))]]))

;; some options to include
;; - ignore case (true/false)
;; - ignore stopwords (true/false)
;;
;; Stats to display
;; - total words
;; - total unique words
;; - longest word
;; - TODO average word length


(defn main-panel []
  (let [results @(re-frame/subscribe [::subs/results])]
    [:div {:class "flex flex-col justify-center mx-64"}
     [:div
      [:h1 {:class "pt-5 mb-4 text-2xl font-extrabold text-gray-900 dark:text-white md:text-5xl lg:text-4xl"}
       [:span {:class "text-transparent bg-clip-text bg-gradient-to-r to-emerald-600 from-sky-400"} "Word Frequency "] "Calculator"]]
     [input-form]
     (when results
       [:div {:class "mt-5 p-5 border border-indigo-600"}
        [results-display results]])]))
