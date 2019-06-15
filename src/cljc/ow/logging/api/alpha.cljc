(ns ow.logging.api.alpha
  (:refer-clojure :rename {defn defn-clj
                           fn   fn-clj
                           let  let-clj})
  #?(:cljs (:require-macros [ow.logging.clojure :as cljm]
                            [ow.logging.macros :as mm]))
  (:require [ow.logging.log :as l]
            [ow.logging.clojure :as clj]
            [ow.logging.macros :as mm]
            [ow.logging.meta :as mt]))

(defmacro with-trace
  "Adds an entry into the current trace history."
  [name & body]
  `(mm/with-trace ~name ~@body))

(defmacro with-trace-data
  "Adds user data into the current trace info map that will be available in subsequent log invocations."
  [data & body]
  `(mm/with-trace-data ~data ~@body))

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
  (mt/attach obj))

(defn-clj detach
  "Returns the attached trace info map from obj."
  [obj]
  (mt/detach obj))

(defmacro with-loginfo
  "Sets the current trace info map to loginfo."
  [loginfo & body]
  `(mtm/with-loginfo ~loginfo ~@body))

(defmacro fn
  "Same as clojure.core/fn, but also adds an entry into the current trace history upon invocation of the fn."
  [name [& args] & body]
  `(cljm/fn ~name [~@args] ~@body))

(defmacro defn
  "Same as clojure.core/defn, but also adds en entry into the current trace history upon invocation of the defn."
  [name [& args] & body]
  `(cljm/defn ~name [~@args] ~@body))

(defmacro let
  "Like clojure.core/let, but also sets the current trace info map to
  potential trace info maps that might be attached to the given values."
  [[& bindings] & body]
  `(cljm/let [~@bindings] ~@body))
