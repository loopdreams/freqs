(ns freqs.frequencies
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]))


(def stopwords #{"i" "me" "my" "myself" "we" "our" "ours" "ourselves" "you" "your" "yours" "yourself" "yourselves" "he" "him" "his" "himself" "she" "her" "hers" "herself" "it" "its" "itself" "they" "them" "their" "theirs" "themselves" "what" "which" "who" "whom" "this" "that" "these" "those" "am" "is" "are" "was" "were" "be" "been" "being" "have" "has" "had" "having" "do" "does" "did" "doing" "a" "an" "the" "and" "but" "if" "or" "because" "as" "until" "while" "of" "at" "by" "for" "with" "about" "against" "between" "into" "through" "during" "before" "after" "above" "below" "to" "from" "up" "down" "in" "out" "on" "off" "over" "under" "again" "further" "then" "once" "here" "there" "when" "where" "why" "how" "all" "any" "both" "each" "few" "more" "most" "other" "some" "such" "no" "nor" "not" "only" "own" "same" "so" "than" "too" "very" "s" "t" "can" "will" "just" "don" "should" "now"})

(def stopwords-case (into #{} (map str/capitalize stopwords)))

(defn split-words [string]
  (re-seq #"\w+" string))

(defn count-word-frequencies [text & case?]
  (if-not case?
    (->> text
         split-words
         (map str/lower-case)
         frequencies)
    (->> text
         split-words
         frequencies)))

(defn remove-stopwords [freqs]
  (apply (partial dissoc freqs) (merge stopwords stopwords-case)))

(defn remove-numbers [freqs]
  (let [numbers (filter #(re-find #"^\d+$" %) (keys freqs))]
    (apply (partial dissoc freqs) (into #{} numbers))))

(defn remove-single-chars [freqs]
  (let [single-chars (filter #(= (count %) 1) (keys freqs))]
    (apply (partial dissoc freqs) (into #{} single-chars))))


;; Stats

(defn word-count [freqs] (reduce + (vals freqs)))

(defn unique-wrods [freqs] (count freqs))

(defn longest-words [freqs n-words]
  (->> freqs
       (sort-by (fn [[k _]] (count k)))
       reverse
       (take n-words)
       (map first)
       (map (fn [w] [w (count w)]))))

(defn avg-word-length [freqs]
  (let [wc (count (keys freqs))
        total-len (reduce #(+ %1 (count %2)) 0 (keys freqs))]
    (/ total-len wc)))
