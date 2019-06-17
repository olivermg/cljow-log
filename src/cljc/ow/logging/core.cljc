(ns ow.logging.core
  (:require [clojure.string :as s]))

(def ^:dynamic +logging-info+ {:checkpoints []})

(def MAX_INT #?(:clj  Integer/MAX_VALUE
                :cljs (.. js/Number -MAX_SAFE_INTEGER)))

#?(:cljs (set! (.-stackTraceLimit js/Error) 50))

(defn pr-str-map-vals [m]
  (->> m
       (map (fn [[k v]]
              [k (pr-str v)]))
       (into {})))

(defn current-ste-info []
  #?(:clj (let [st (some-> (Throwable.) .getStackTrace seq)
                ste (loop [[ste & st] st]
                      (let [classname (.getClassName ste)]
                        (if (or (s/starts-with? classname "ow.logging")
                                (s/starts-with? classname "clojure"))
                          (recur st)
                          ste)))
                [ns fn file line] (if ste
                                    (concat (s/split (.getClassName ste) #"\$" 2)
                                            [(.getFileName ste) (.getLineNumber ste)])
                                    ["?" "?" "?" "?"])]
            {:file file
             :fn   fn
             :line line
             :ns   ns
             :time (java.util.Date.)})

     :cljs (letfn [(dissect [ste]
                     (some->> ste
                              (re-find #"at[\s]+([^\s]+)\$([^\s]+)[\s]+.*\(([^\(\s]+):([0-9]+):[0-9]+\)")
                              rest))

                   (is-system? [ns]
                     (or (s/starts-with? ns "ow$logging")
                         #_(s/starts-with? ns "figwheel$")
                         #_(s/starts-with? ns "cljs$")
                         #_(s/starts-with? ns "Object.cljs$")
                         #_(s/starts-with? ns "Function.cljs$")))]

             (let [st  (some-> (js/Error.) (.-stack) (s/split "\n"))
                   _ (println st)
                   [ns fn file line :as ste] (or (loop [[ste & st] st]
                                                   (when ste
                                                     (if-let [[ns fn file line :as dissected] (dissect ste)]
                                                       (if-not (is-system? ns)
                                                         dissected
                                                         (recur st))
                                                       (recur st))))
                                                 ["?" "?" "?" "?"])]
               {:file file
                :fn   fn
                :line line
                :ns   ns
                :time (js/Date.)}))))

(defn make-checkpoint* [name & args]  ;; TODO: create record for checkpoint, to prevent overly verbose printing (e.g. of large arguments)
  (-> {:id   (rand-int MAX_INT)
       :name name}
      (merge (current-ste-info))
      (into  [(when-not (empty? args)
                [:args (map pr-str args)])])))

(defn merge-logging-info [logging-info1 logging-info2]
  (update logging-info2 :checkpoints #(-> (concat (:checkpoints logging-info1) %)
                                          vec)))

(defn merge-logging-infos [& logging-infos]
  (reduce merge-logging-info logging-infos))

(defn logging-info []
  +logging-info+)

(defmacro with-logging-info [logging-info & body]
  `(binding [c/+logging-info+ (c/merge-logging-info ~logging-info c/+logging-info+)]
     ~@body))
