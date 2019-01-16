(ns dv.fw.client.ui
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [reagent-forms.core :as rf
                                :refer [bind-fields init-field value-of]]
            cljsjs.bootstrap
            cljsjs.jquery
            jquery.treetable))

(def counter (atom 0))
 (defn make-key []
  (str (swap! counter
              inc)))

(defn widget [widget-type & body]
  (let [[params body] (if (map? (first body))
                        [(first body)
                         (rest body)]
                        [{}
                         body])
        params (assoc params
                 :key (or (:key params)
                          (make-key)))]
    (into [widget-type params]
          (map #(if (sequential? %)
                 (apply widget %)
                 %)
               body))))

;;; simple widgets - implement WidgetParams ;;

(defn button [label & [params icon command tooltip]]
  [:button.btn
   (merge {:on-click command
           :title tooltip} params)
   (if icon
     (if (and icon
              (= "glyphicon"
                 (subs icon 0 9)))
       [:span (merge {:class (str "glyphicon "
                                  icon)}
                     params)]
       [:img {:src icon
              :width 17
              :height 17}]))
   (if icon
     [:span.hidden-xs.hidden-sm label]
     [:span label])])

;;; toolbar with buttons and submenu ;;;

(defn tool-menu [label childWidgets & [params icon]]       ;[tool-button & [params & menu-items]]
  [:div.btn-group {:id label}
   [:button.btn.btn-sm.btn-default.dropdown-toggle {:type "button"
                                                    :data-toggle "dropdown"}
    [:span.hidden-xs.hidden-sm label] [:span.caret]
      (if icon
        [:img {:src icon
               :width 17
               :height 17}])]
   (conj [:ul.dropdown-menu params]
         [:li (for [c childWidgets]
                c)])])

(defn tool-bar [& [params & widgets]]               ;; widgets is a vector of buttons, menus etc.
  [:nav (merge {:role "navigation"
                :id :tool-bar} params)
   [:div.container-fluid
    (conj [:div.btn-toolbar.btn-group]
          (for [w widgets]
            (if (= (type (nth w 1)) cljs.core/PersistentArrayMap)
              (assoc w 1 (merge (nth w 1) {:key (str "tool-bar-widget-" w)}))
              w)))]])

;;; advanced widgets - implement WidgetParams, WidgetValue, WidgetValueSelection ;;;

(defn label [text & [params]]                               ;; maybe in components
  [:div params text])

(defn image [image & [params tooltip]]
  [:img.img-responsive (merge {:id (if (not= nil tooltip)
                                       tooltip
                                         "image")
                              :src image
                              :title tooltip} params)])

(defn single-line-string [id & [params tooltip]]                 ;;textfield ;; ID is given as a keyword
  [:input.form-control (merge {:field :text
                               :id id
                               :title tooltip} params)])

(defn multi-line-string [id & [params tooltip]]                  ;;textarea
  [:textarea.form-control (merge {:field :textarea
                                  :id id
                                  :title tooltip} params)])

(defn check-box [id label-text & [params tooltip]]
  [:div.row
   [:div.col-md-2 [:label label-text]]
   [:div.col-md-5
    [:input.checkbox (merge {:field :checkbox
                             :id id
                             :title tooltip} params)]]])

(defn spinner [id min max & [params tooltip]]
  [:input.form-control (merge {:field :numeric
                               :type :number
                               :id id
                               :min min
                               :max max
                               :title tooltip} params)])



(defn select-box [id labelList & [params]]    ;;dropdown list (maybe with search function, suggestions) ;; in Atom we need to put ID's
  [:div (merge {:field           :typeahead
                :id              id
                :data-source     (fn [text]
                                   (filter
                                     #(-> % (.toLowerCase %) (.indexOf text) (> -1))
                                     labelList))
                :input-class     "form-control"
                :list-class      "typeahead-list"
                :item-class      "typeahead-item"
                :highlight-class "highlighted"} params)])

(defn list-box [idLabelList selectedElementId & [params]]   ;; SORTED elements
  [:div.container
   [:div.list-group params
    (let [sorted-elements (sort-by second idLabelList)]
     (for [element sorted-elements]
      (let [list-item [:a {:key (str "list-box-" (first element))
                             :id (first element)
                             :href "#"
                             :class "list-group-item"} (second element)]]
          (if (= (first element) selectedElementId)
              (assoc list-item 1 (merge (second list-item) {:class "list-group-item active"}))
              list-item))))]])

(defn date-field [id & [params tooltip]]
  [:input.form-control
          (merge {:field :datepicker
                  :id id
                  :date-format "yyyy/mm/dd"
                  :inline false
                  :title tooltip
                  :in-fn #(when % {:year (.getFullYear %) :month (.getMonth %) :day (.getDate %)})
                  :out-fn #(when % (js/Date. (:year %) (:month %) (:day %)))} params)])

(defn date-time-field [id & [params tooltip]]
  [:input.form-control
          (merge {:field :text
                  :type  :datetime-local
                  :id    id
                  :title tooltip
                  :in-fn #(when % {:year (.getFullYear %) :month (.getMonth %) :day (.getDate %)})
                  :out-fn #(when % (js/Date. (:year %) (:month %) (:day %)))} params)])

(defn table-head [_ columnLabels]
  [:thead>tr nil
   (for [label columnLabels]
     [:th {:key label} label])])

(defn table-row [columnKeys row & [selection listener attributes]]
  [:tr (merge attributes {:onClick #(do (when selection
                                          (swap! selection
                                                 (fn [curr]
                                                   ((if (contains? curr
                                                                   row)
                                                      disj
                                                      conj) curr row))))
                                        (when listener
                                          (listener)))}
              (when (and selection
                         (contains? @selection
                                    row))
                {:class "info"}))
   (for [key columnKeys]
     (let [content (get row
                        key)]
       [:td {:key key}
        (if (vector? content)
          content
          (str content))]))])

;; filtering/sorting does not work with bootstrap-table as it handles content by itself + does not work
;; https://github.com/reagent-project/reagent-cookbook/tree/master/recipes/sort-table
;; https://github.com/reagent-project/reagent-cookbook/tree/master/recipes/filter-table

(defn table
  ([header rows]
   [:div.table-responsive
    [:table.table.table-condensed.table-striped.table-bordered.table-hover
            {:style {:borderCollapse "collapse"
                     :padding "0px"}
             :cell-spacing "0"}
            header
            (into [:tbody]
                  rows)]])
  ([columnKeys columnLabels data & [params listener selection]]
   {:pre [(not (nil? @data))
          (or (nil? selection)
              (not (nil? @selection)))
          (or (nil? listener)
              (fn? listener))]}

   (table (table-head columnKeys columnLabels)
          (for [row @data]
            (table-row columnKeys row selection listener)))))


(defn create-tree-table-node [nodelist parent-id]
              (apply concat (for [[indx node] (map-indexed vector nodelist)]
                              (let [id (str parent-id "-" indx)
                                row (apply conj [:tr {:key (str "tr-node-" id)
                                                      :class (str "treegrid-" id " treegrid-parent-" parent-id)}]
                                        (for [dd (:data node)]
                                          [:td {:id (first dd)
                                                :key (str "td-node-" (second dd))} (second dd)]))]
                                (concat [row] (if (:children node)
                                                (create-tree-table-node (:children node) id)))))))

(defn tree-table [columnKeys columnLabels data & [params]] ; implements also TableFilter
  (r/create-class {:component-did-mount (fn [this] (.treegrid (js/$ (r/dom-node this))))
                   :reagent-render      #(do [:table (merge {:class "table tree"
                                                                     :id    "tree-table"} params)
                                              (table-head columnKeys columnLabels)
                                              (let [content (create-tree-table-node data nil)]
                                                (conj [:tbody] content))])}))
(defn composite [& [params & widgets]]
  [:div {:class "panel panel-default"}
   (conj [:div.panel-body params]
         widgets)])

;; other - do not implement any protocol


(defn tab-view [pages selectedPageName vertical? & [params]] ; pages -> [[name create-fn & [icon]] [name create-fn & [icon]])
                                                                         ; deleted barPosition - position will be set by bootstrap class you will apply in params
  [:div.container {:key (str "tab-view-" selectedPageName)}
   (let [container [:ul.nav.nav-tabs (merge {:key (str "tab-view-ul-" selectedPageName)} params)
                    (for [[name _ icon] pages]
                      (let [a-ul [:a {:key (str "tab-view-ul-a-" name)
                                      :data-toggle "tab"
                                      :href (str "#" (first (str/split name #" ")))} (if icon
                                                                                       [:img {:src icon}]
                                                                                       name)]]
                        (if (= selectedPageName name)
                          [:li.active {:key (str "tab-view-ul-li-" name)} a-ul]
                          [:li {:key (str "tab-view-ul-li-" name)} a-ul])))]]
     (if vertical?
       [:nav.col-xs-3 (assoc container 0 :ul.nav.nav-tabs.tabs-left)]
       container))
   (let [tab-content [:div.tab-content {:key (str "tab-view-tab-content-" selectedPageName)}
                      (for [pag pages]
                        (let [name (first pag)
                              create-fn (nth pag 1)]
                          (if (= selectedPageName name)
                            [:div.tab-pane.active {:key (str "tab-view-tab-content-tab-pane-" pag)
                                                   :id (first (str/split name #" "))} create-fn]
                            [:div.tab-pane {:key (str "tab-view-tab-content-tab-pane-" pag)
                                            :id (first (str/split name #" "))} create-fn])))]]
     (if vertical?
       [:div.col-xs-9 tab-content]
       tab-content))])

(defn native-widget [customFn & [params]]
    (r/create-class {:component-did-mount (fn [this](customFn (r/dom-node this)))
                     :reagent-render #(do [:div (merge {:key (str "native-widget-" customFn)} params)])}))

(defn dialog [popup-shown title composite buttons]
  [(r/create-class {:component-did-update (fn [this]
                                            (when-let [node (r/dom-node this)]
                                              (.modal (js/$ node))))
                    :component-will-update (fn [this]
                                             (when-let [node (r/dom-node this)]
                                               (.modal (js/$ node)
                                                       "hide")))
                    :reagent-render (fn []
                                      (when @popup-shown
                                        (let [status (r/atom nil)]
                                          [:div.modal.fade {:role "dialog"
                                                            :data-backdrop "static"}
                                           [:div.modal-dialog {:role "document"}
                                            [:div.modal-content
                                             [:div.modal-header
                                              [:h4.modal-title.col.xs-11 title]]
                                             [:div.modal-body {:overflow "auto"}
                                              composite
                                              [identity @status]]
                                             (into [:div.modal-footer]
                                                   (map (fn [button]
                                                          (let [[_ {:keys [on-click] :as attributes}] button]
                                                            (update-in button
                                                                       [1 :on-click]
                                                                       (fn [old-fn]
                                                                         #(try
                                                                           (when old-fn
                                                                             (old-fn))
                                                                           (reset! popup-shown
                                                                                   false)
                                                                           (catch js/Object e
                                                                             (reset! status
                                                                                     (str e))))))))
                                                        buttons))]]])))})])

(defn message-box [popup-shown title composite & [level]] ; level can be :info :warn :error
  (dialog popup-shown
          title
          [:div
           [:span.glyphicon.col-xs-1 {:class (cond level
                                                   :info "glyphicon-info-sign"
                                                   :warn "glyphicon-warning-sign"
                                                   :error "glyphicon-minus-sign"
                                                   "glyphicon-info-sign")
                                      :style {:font-size "200%"
                                              :color "orange"}}]
           [:div.col-xs-11 composite]]
          [(button "Ok" {:class "btn-primary"} "glyphicon-ok")]))

(defn ok-cancel-dialog [popup-shown title editor callbackOk & [callbackCancel windowParams]]
  (dialog popup-shown title editor
          [(button "Ok" {:class "btn-primary"} "glyphicon-ok" callbackOk )
           (button "Cancel" {:class "btn-default"} "glyphicon-remove" callbackCancel)]))



(defn scroll-composite [composite & height]
  [:div {:style {:overflow-y :scroll
                 :height (if height
                           height
                           "100px")}} composite])

(defn set-root [composite]
  )
