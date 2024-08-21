(ns freqs.views
  (:require
   [re-frame.core :as re-frame]
   [freqs.subs :as subs]
   [freqs.events :as events]
   [freqs.frequencies :as f]
   [goog.string :as gstring]
   [goog.string.format]))


(defn input-form []
  [:div
   [:div
    [:textarea {:class "block p-2.5 w-full text-sm text-gray-900 bg-gray-50 rounded-lg border border-gray-300 focus:ring-blue-500 focus:border-blue-500 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                :rows 5
                :cols 80
                :placeholder "Paste your text here"
                :on-change #(re-frame/dispatch [::events/update-text-input (-> % .-target .-value)])}]]
   [:div
    [:button {:class "mt-6 select-none rounded-lg bg-blue-500 py-3 px-6 text-center align-middle font-sans text-xs font-bold uppercase text-white shadow-md shadow-blue-500/20 transition-all hover:shadow-lg hover:shadow-blue-500/40 focus:opacity-[0.85] focus:shadow-none active:opacity-[0.85] active:shadow-none disabled:pointer-events-none disabled:opacity-50 disabled:shadow-none"
              :on-click #(re-frame/dispatch [::events/get-word-frequencies])}
     "Get word frequencies"]]])

(defn result-filter-item [label event & opt]
  [:div {:class "flex items-center mb-4"}
   [:input {:type "checkbox"
            :class "w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600"
            :on-change #(re-frame/dispatch
                         (if opt
                           [event (first opt) (-> % .-target .-checked)]
                           [event (-> % .-target .-checked)]))}]
   [:label {:class "ms-2 text-sm font-medium text-gray-900 dark:text-gray-300"} label]])

(defn results-filters []
  [:div
   [result-filter-item "Remove stopwords" ::events/update-result-filters :stopwords]
   [result-filter-item "Remove numbers" ::events/update-result-filters :numbers]
   [result-filter-item "Remove single-character words" ::events/update-result-filters :single-chars]
   [result-filter-item "Case sensitive?" ::events/get-word-frequencies]])


(defn stat-component [title value]
  [:div {:class "flex justify-between w-64 py-2"}
   [:p {:class "font-semibold pr-5"} title]
   [:p {:class "text-fuchsia-700 pr-5"} value]])

(defn longest-words [results]
  (into
       [:div {:class "flex flex-wrap py-2"}]
       (for [w    (f/longest-words results 5)
             :let [[word length] w]]
         [:p {:class "bg-blue-100 text-blue-800 text-sm font-medium me-2 px-2.5 py-0.5 m-1 rounded dark:bg-blue-900 dark:text-blue-300"}
          (str word " (" length ")")])))

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
    [:div {:class "group flex flex-row py-1"}
     [:span
      {:class "mr-2 opacity-0 group-hover:opacity-100 transition-opacity bg-fuchsia-100 text-black-400 px-2 py-3 text-sm rounded-md mx-auto"}
      "Copy unfiltered results data with csv formatting"]
     [:button
      {:class "font-medium text-center transition-all disabled:opacity-50 disabled:shadow-none disabled:pointer-events-none w-10 max-w-[40px] h-10 max-h-[40px] rounded-lg text-xs shadow-md shadow-gray-900/10 hover:shadow-lg hover:shadow-gray-900/20 focus:opacity-[0.85] focus:shadow-none active:opacity-[0.85] active:shadow-none"
       :on-click #(do (.stopPropagation %)
                      (copy-to-clipboard csv))}
      [:span {:class "far fa-copy text-base"}]]]))

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
  (let [stopwords?    @(re-frame/subscribe [::subs/result-filters :stopwords])
        numbers?      @(re-frame/subscribe [::subs/result-filters :numbers])
        single-chars? @(re-frame/subscribe [::subs/result-filters :single-chars])
        results       (cond-> results
                        stopwords? f/remove-stopwords
                        numbers?   f/remove-numbers
                        single-chars? f/remove-single-chars)
        section-break [:hr {:class "my-6"}]]
    [:div
     [results-filters]

     section-break

     [:div
      [:h2 {:class "text-2xl font-bold py-2"} "Stats"]
      [stat-component "Number of Words" (f/word-count results)]
      [stat-component "Number of Unique Words" (f/unique-wrods results)]
      [stat-component "Average Word Length" (gstring/format "%.2f" (f/avg-word-length results))]
      [:p {:class "font-semibold pr-5 py-2"} "Longest Words:"]
      [longest-words results]]

     section-break

     [:div
      [:div {:class "flex justify-between"}
       [:h2 {:class "text-2xl font-bold pt-2"} "Word Frequencies"]
       [copy-table-data]]
      [word-frequencies-table results]]]))

(def site-title
  [:div
      [:h1 {:class "pt-5 mb-4 text-3xl font-extrabold text-gray-900 dark:text-white md:text-4xl lg:text-4xl"}
       "Word"
       [:span {:class "text-transparent bg-clip-text bg-gradient-to-r to-cyan-900 from-sky-400"}
        " Frequencies"]]])

(def site-footer
  (let [link-style "text-blue-600 dark:text-blue-500 hover:underline"]
    [:footer {:class "sticky top-[100vh]"}
     [:div {:class "w-full mx-auto py-2 flex flex-row justify-center space-x-2 mb-2 rounded text-sm border-t-2 text-slate-500"}
      [:p [:a {:href "https://eoin.site"
               :class link-style} "eoin.site"]]
      [:p "|"]
      [:p "Source code available on " [:a {:href "https://github.com/loopdreams/freqs"
                                           :class link-style} "Github"]]]]))

(defn main-panel []
  (let [results @(re-frame/subscribe [::subs/results])]
    [:div {:class "flex flex-col justify-center max-w-prose m-auto min-h-screen px-2"}
     site-title
     [:p {:class "pb-5 text-slate-500"} "A simple tool for counting the frequencies of words in a piece of text."]
     [input-form]
     (when results
       [:div {:class "my-5 p-5 sm:border sm:border-indigo-600"}
        [results-display results]])
     site-footer]))
