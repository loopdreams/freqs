(ns freqs.views
  (:require
   [re-frame.core :as re-frame]
   [freqs.subs :as subs]
   [freqs.events :as events]
   [freqs.frequencies :as f]))

(defn input-form []
  (let [text-input @(re-frame/subscribe [::subs/text-input])]
    [:div {:class "px-4"}
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
   [:p {:class "text-fuchsia-700 pr-5"} value]])

(defn copy-to-clipboard [val]
  (let [el (js/document.createElement "textarea")]
    (set! (.-value el) val)
    (.appendChild js/document.body el)
    (.select el)
    (js/document.execCommand "copy")
    (.removeChild js/document.body el)))

(defn data->csv-string [results]
  (reduce (fn [csv [word fq]]
            (if (empty? csv)
              (str "Word,Count\r\n"
                   (str word "," fq))
              (str csv "\r\n" (str word "," fq))))
          ""
          results))

;; TODO consider wheather to sort here and whether to respect other filters (e.g., ignore stopwords)
(defn copy-table-data []
  (let [data @(re-frame/subscribe [::subs/results])
        csv (data->csv-string data)]
    [:div {:class "group"}
     [:button
      {:class "relative align-middle select-none font-sans font-medium text-center uppercase transition-all disabled:opacity-50 disabled:shadow-none disabled:pointer-events-none w-10 max-w-[40px] h-10 max-h-[40px] rounded-lg text-xs shadow-md shadow-gray-900/10 hover:shadow-lg hover:shadow-gray-900/20 focus:opacity-[0.85] focus:shadow-none active:opacity-[0.85] active:shadow-none"
       :href "javascript:"
       :on-click #(do (.stopPropagation %)
                      (copy-to-clipboard csv))}
      [:span {:class "far fa-copy text-base"}]]
     [:span
      {:class "group-hover:opacity-100 transition-opacity bg-gray-800 px-1 text-sm text-gray-100 rounded-md absolute -translate-y-10 -translate-x-28 opacity-0 m-4 mx-auto"}
      "Copy table data with csv formatting"]]))

;; (defn copyable-table [value]
;;   [:a.tooltip.is-tooltip-left
;;    {:href "javascript:"
;;     :data-tooltip "Click to Copy"
;;     :on-click #(do (.stopPropagation %)
;;                    (copy-to-clipboard value))}
;;    [:div "Click to Copy"]])


(defn word-frequencies-table [results]
  (into [:table {:class "w-full text-sm text-left rtl:text-right text-gray-500"}]
        [[:thead {:class "text-xs text-gray-900 uppercase bg-gray-50"}
          [:tr {:style {:user-select "text"}}
           [:th {:class "px-6 py-3"} "Word"]
           [:th {:class "px-6 py-3"}"Word Count"]]]
         (into [:tbody]
               (map (fn [[word fq]] [:tr {:class "bg-white"}
                                     [:td {:class "px-6 py-2"} word]
                                     [:td {:class "px-6 py-2"} fq]])
                    (reverse (sort-by val results))))]))


(defn results-display [results]
  (let [stopwords? @(re-frame/subscribe [::subs/stopwords?])
        results (if stopwords?
                  results
                  (f/remove-stopwords results))]
    [:div
     [results-filters]
     [:hr {:class "my-6"}]
     [:div
      [:h2 {:class "text-2xl font-bold py-2"} "Stats"]
      [stat-component "Number of Words" (f/word-count results)]
      [stat-component "Number of Unique Words" (f/unique-wrods results)]
      [:p {:class "font-bold pr-5 py-2"} "Longest Words:"]
      (into
       [:div {:class "flex flex-wrap py-2"}]
       (for [w (f/longest-words results 5)
             :let [[word length] w]]
         [:p {:class "bg-blue-100 text-blue-800 text-sm font-medium me-2 px-2.5 py-0.5 m-1 rounded dark:bg-blue-900 dark:text-blue-300"}
          (str word " (" length ")")]))]
     [:hr {:class "my-6"}]
     [:div
      [:div {:class "flex justify-between"}
       [:h2 {:class "text-2xl font-bold pb-4"} "Word Frequencies"]
       [copy-table-data]]
      [word-frequencies-table results]]]))

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
    [:div {:class "flex flex-col justify-center max-w-prose m-auto"}
     [:div
      [:h1 {:class "pl-4 pt-5 mb-4 text-2xl font-extrabold text-gray-900 dark:text-white md:text-5xl lg:text-4xl"}
       [:span {:class "text-transparent bg-clip-text bg-gradient-to-r to-emerald-600 from-sky-400"} "Word Frequency "] "Calculator"]]
     [input-form]
     (when results
       [:div {:class "my-5 p-5 sm:border sm:border-indigo-600"}
        [results-display results]])]))
