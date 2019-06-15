(ns ow.logging.api.alpha
  (:refer-clojure :rename {defn defn-clj
                           fn   fn-clj
                           let  let-clj})
  (:require [ow.logging :as l]
            [ow.logging.clojure :as lc]
            [ow.logging.meta :as lm]))

(defmacro with-trace
  "Adds an entry into the current trace history."
  [name & body]
  `(l/with-trace ~name ~@body))

(defmacro with-trace-data
  "Adds user data into the current trace info map that will be available in subsequent log invocations."
  [data & body]
  `(l/with-trace-data ~data ~@body))

(defn-clj get-trace 
  "Returns the current trace history."
  []
  (l/get-trace))

(defn-clj get-trace-root
  "Returns the root/first/topmost entry in the current trace history."
  []
  (l/get-trace-root))

(defn-clj log
  "Prints a log message based on the current trace info map."
  [level msg & [data]]
  (l/log level msg data))

(defn-clj trace [msg & [data]]
  (l/trace msg data))

(defn-clj debug [msg & [data]]
  (l/debug msg data))

(defn-clj info [msg & [data]]
  (l/info msg data))

(defn-clj warn [msg & [data]]
  (l/warn msg data))

(defn-clj error [msg & data]
  (l/error msg data))

(defn-clj fatal [msg & [data]]
  (l/fatal msg data))

(defn-clj attach
  "Attaches the current trace info map as metadata to obj. Will not do anything if obj is
   not an instance of IObj, because metadata cannot be attached in this case."
  [obj]
  (lm/attach obj))

(defn-clj detach
  "Returns the attached trace info map from obj."
  [obj]
  (lm/detach obj))

(defmacro with-loginfo
  "Sets the current trace info map to loginfo."
  [loginfo & body]
  `(lm/with-loginfo ~loginfo ~@body))

(defmacro fn [& args]
  `(lc/fn ~@args))
(defmacro defn [& args]
  `(lc/defn ~@args))
(defmacro let [& args]
  `(lc/let ~@args))
