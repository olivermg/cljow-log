(ns ow.logging.core
  #?(:cljs (:require-macros [ow.logging.core :refer [#_with-initialized-logging #_with-instance with-historical-logging-info
                                                     #_with-logging-info with-checkpoint* with-checkpoint with-data
                                                     #_with-logging with-appended with-prepended with-replaced]]))
  (:require [clojure.string :as s]))

(def MAX_INT #?(:clj  Integer/MAX_VALUE
                :cljs (.. js/Number -MAX_SAFE_INTEGER)))

#?(:clj  (def ^:dynamic +logging-info+ {:checkpoints []}))

#?(:cljs (def domain (js/require "domain")))
#?(:cljs (set! (.-stackTraceLimit js/Error) 50))

(defn pr-str-map-vals [m]
  (->> m
       (map (fn [[k v]]
              [k (pr-str v)]))
       (into {})))

(defn current-ste-info []
  #?(:clj (let [st                (some-> (Throwable.) .getStackTrace seq)
                ste               (loop [[ste & st] st]
                                    (let [classname (.getClassName ste)]
                                      (if (or (s/starts-with? classname "ow.logging")
                                              (s/starts-with? classname "clojure"))
                                        (recur st)
                                        ste)))
                [ns fn]           (when ste
                                    (s/split (.getClassName ste) #"\$" 2))
                fn                (when fn
                                    (some->> (s/split fn #"\$")
                                             (remove #(s/starts-with? % "__hide"))
                                             (s/join "$")))
                [file line]       (when ste
                                    [(.getFileName ste) (.getLineNumber ste)])
                [ns fn file line] [(or ns "?") (or fn "?") (or file "?") (or line "?")]]
            {:file file
             :fn   fn
             :line line
             :ns   ns
             :time (java.util.Date.)})

     :cljs (letfn [(dissect [ste]
                     (when-let [[location file line] (some->> ste
                                                              (re-find #"at[\s]+([^\s]+)[\s]+.*\(([^\(\s]+):([0-9]+):[0-9]+\)")
                                                              rest)]
                       (let [location (or (some->> location
                                                   (re-find #"(.+?)\$___hide.*")
                                                   second)
                                          location)]
                         [location file line])))

                   (is-system? [location]
                     (or (re-find #"ow.logging" location)
                         #_(re-matches #"[^\$]+" location)
                         (re-matches #"[A-Z][a-zA-Z0-9-_]+\.cljs\$.+" location)
                         (s/starts-with? location "figwheel$")
                         (s/starts-with? location "cljs$core")
                         #_(s/starts-with? location "Object.cljs$")
                         #_(s/starts-with? location "Function.cljs$")))]

             (let [st  (some-> (js/Error.) (.-stack) (s/split "\n"))
                   [location file line :as ste] (or (loop [[ste & st] st]
                                                      (when ste
                                                        (if-let [[location file line :as dissected] (dissect ste)]
                                                          (if-not (is-system? location)
                                                            dissected
                                                            (recur st))
                                                          (recur st))))
                                                    ["?" "?" "?" "?"])]
               {:file     file
                :fn       location
                :line     line
                :ns       location
                :time     (js/Date.)}))))

(defn make-checkpoint [name & args]  ;; TODO: create record for checkpoint, to prevent overly verbose printing (e.g. of large arguments)
  (let [name (if (symbol? name)
               (str name)
               name)]
    (-> {:id   (rand-int MAX_INT)
         :name name}
        (merge (current-ste-info))
        (into  [(when-not (empty? args)
                  [:args (map pr-str args)])]))))

#_(defn append-checkpoints [logging-info checkpoints]
  (update logging-info :checkpoints #(-> (concat % checkpoints) vec)))

#_(defn append-checkpoint [logging-info checkpoint]
  (update logging-info :checkpoints conj checkpoint))

#_(defn prepend-checkpoints [logging-info checkpoints]
  (update logging-info :checkpoints #(-> (concat checkpoints %) vec)))

#_(defn prepend-checkpoint [logging-info checkpoint]
  (prepend-checkpoints logging-info [checkpoint]))

;;; TODO: also merge current data, not just the checkpoints:
#_(defn merge-historical-logging-info [logging-info1 logging-info2]
  (prepend-checkpoints logging-info2 (:checkpoints logging-info1)))

#_(defn merge-historical-logging-infos [& logging-infos]
  (reduce merge-historical-logging-info logging-infos))

(defn current-logging-info []
  #?(:clj  +logging-info+
     :cljs (or (some-> (.-active domain)
                       (.-logging-info))
               {:checkpoints []})))

(defn get-checkpoints []
  (get-in (current-logging-info) [:checkpoints]))

(defn get-root-checkpoint []
  (get (get-checkpoints) 0))



;;; NOTE: macros always get compiled in java/jvm, even for cljs code, because they are compile time citizens.
;;;   this implies that we need to define them in .clj or .cljc files, not in .cljs files.
;;;   and we need to require them via :require-macros in cljs code.
;;;
;;;   you can define and use macros in one single cljc file in cljs by declaring in the ns expr sth like:
;;;   (:require-macros [macro-ns :refer [macro1 macro2])
;;;   this way it even works seamlessly in clj too, as in clj you can directly refer to the macros by
;;;   their unqualified names.

#_(defn initialize-logging! [cb]
  (do #?(:clj  (cb)
         :cljs (doto (.create domain)
                 (.run cb)))
      nil))

#_(defmacro with-initialized-logging [& body]
  `(initialize-logging!
     (fn __hide# [] ~@body)))

#_(defn init-logging! [cb]
  #?(:clj  (cb)
     :cljs (let [prev-domain    (.-active domain)
                 old-info       (when prev-domain
                                  (.-logging_info prev-domain))
                 _ (println "bbb" old-info)
                 current-domain (doto (.create domain)
                                  (.enter))]
             (when old-info
               (set! (.-logging_info current-domain) old-info))
             (let [cb-result (cb)]
               (.exit current-domain)
               cb-result))))

#_(defmacro with-logging [& body]
  `(init-logging!
     (fn __hide# [] ~@body)))

#_(defn inject-logging-info! [logging-info cb]
  #?(:clj  (binding [+logging-info+ logging-info]
             (cb))
     :cljs (let [d                 (.-active domain)
                 prev-logging-info (.-logging_info d)
                 _                 (set! (.-logging_info d) logging-info)
                 result            (cb)]
             (set! (.-logging_info d) prev-logging-info)
             result)))

(defn replace! [logging-info cb]
  #?(:clj  (binding [+logging-info+ logging-info]
             (cb))
     :cljs (let [current-domain (doto (.create domain)
                                  (.enter))]
             (set! (.-logging_info current-domain) logging-info)
             (try
               (cb)
               (finally
                 (.exit current-domain))))))

(defmacro with-replaced [logging-info & body]
  `(replace! ~logging-info
     (fn __hide# [] ~@body)))

(defn prepend! [checkpoints data cb]
  (let [logging-info (-> (current-logging-info)
                         (update :checkpoints #(-> (concat checkpoints %) vec))
                         (update :data #(merge (pr-str-map-vals data) %)))]
    (replace! logging-info cb)))

(defmacro with-prepended [checkpoints data & body]
  `(prepend! ~checkpoints ~data
     (fn __hide# [] ~@body)))

(defn append! [checkpoints data cb]
  (let [logging-info (-> (current-logging-info)
                         (update :checkpoints #(-> (concat % checkpoints) vec))
                         (update :data #(merge % (pr-str-map-vals data))))]
    (replace! logging-info cb)))

(defmacro with-appended [checkpoints data & body]
  `(append! ~checkpoints ~data
     (fn __hide# [] ~@body)))

#_(defmacro with-logging-info [logging-info & body]
  `(inject-logging-info! ~logging-info
     (fn __hide# [] ~@body)))

(defmacro with-historical-logging-info [logging-info & body]
  `(let [{checkpoints# :checkpoints data# :data} ~logging-info]
     (with-prepended checkpoints# data#
       ~@body)))

(defmacro with-checkpoint* [name [& args] & body]
  `(with-appended [(make-checkpoint ~name ~@args)] {}
     ~@body))

(defmacro with-checkpoint [name & body]
  `(with-checkpoint* ~name []
     ~@body))

(defmacro with-data [data & body]
  `(with-appended [] ~data
     ~@body))



#_(let [domain (js/require "domain")]
  (with-checkpoint check1
    (with-data {:check1 :data1}
      (println "sync1" (.. domain -active -logging_info))
      (js/setTimeout (fn []
                       (with-checkpoint check11
                         (with-data {:check11 :data11}
                           (println "async11" (.. domain -active -logging_info))
                           (js/setTimeout (fn []
                                            (with-checkpoint check111
                                              (with-data {:check111 :data111}
                                                (println "async12" (.. domain -active -logging_info)))))
                                          1000))))
                     1000)
      (js/setTimeout (fn []
                       (with-checkpoint check12
                         (with-data {:check12 :data12}
                           (println "async2" (.. domain -active -logging_info)))))
                     1500))))
