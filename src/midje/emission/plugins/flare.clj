(ns midje.emission.plugins.flare
  (:use midje.clojure.core)
  (:require [midje.util.ecosystem :as ecosystem]
            [midje.emission.plugins.util :as util]))

(defn emit-flare-lines [& args])

(ecosystem/when-1-5+ 
 (require '[flare.core :as flare])
 ;; Following code adapted from
 ;; https://raw.githubusercontent.com/andersfurseth/flare/master/src/flare/midje.clj
 
 (defn report [reports]
   (when (seq reports)
     (util/emit-one-line "")
     (doseq [report reports]
       (util/emit-one-line report))))

 (defn generate-report-for-keyed-diff [[path diffs]]
   (let [diffs (map flare/report diffs)
         indent-diffs? (and (seq path) (< 1 (count diffs)))]
     (-> diffs
         (cond->> indent-diffs? (map #(str "  " %)))
         flare/join-with-newlines
         (cond->> (seq path) (str "in " (pr-str path) (if indent-diffs? "\n" " "))))))

 (defn generate-reports [diffs]
   ; (prn "Flare data: " diffs)
   ; (clojure.pprint/pprint diffs)
   (println "Flare output (experimental):")
   (try
     (->> diffs
          flare/flatten-keys
          sort
          (map flare/generate-report-for-keyed-diff))
     (catch Exception e)))

 (defn emit-flare-lines [failure-map]
   (when (= (:type failure-map) :actual-result-did-not-match-expected-value)
     
     (some-> (flare/diff (:expected-result failure-map) (:actual failure-map))
             generate-reports
             report)))
)
