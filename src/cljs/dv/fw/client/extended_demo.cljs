(ns dv.fw.client.extended-demo
  (:require [dv.fw.client.ui :as ui]
            [reagent.core :as r]
            [reagent-forms.core :as rf
             :refer [bind-fields init-field value-of]]
            [json-html.core :refer [edn->hiccup]]
            cljsjs.jquery
            cljsjs.bootstrap))

;; part 1
;; read about bootstrap, focusing on components and layout
;;      - http://getbootstrap.com/
;;      - http://www.w3schools.com/bootstrap/default.asp
;; test the 5 approaches based on functional reactive programming (frp) by writing the smallest possible function having data that is loaded/updated/unloaded and component lifecycle:
;;      - https://github.com/tonsky/rum
;;      - https://github.com/omcljs/om
;;        - https://github.com/racehub/om-bootstrap
;;        - https://github.com/luxbock/bootstrap-cljs
;;      - https://github.com/reagent-project/reagent
;;      - https://github.com/Day8/re-frame
;;        - https://github.com/Day8/re-com
;;      - https://github.com/levand/quiescent
;; each demo function will be named demo-xxx and it will do the following
;; - list the persons in a table
;; - allow add/edit/remove (accounts are to be ignored)

;; part 2
;; implement functions in ui + transform it to account for changes
;;      - http://bilus.github.io/reforms/ - easy bootstrap ui over react wrappers
;; test the implementation via a demo
;; - list persons in a table
;; - add/edit/delete selected person from table
;; - when editing a person, we list its accounts in a table underneath the other attributes. from this table, we can add/edit/remove elements via another dialog

;; part 3
;; adjustments and functionality that is not included with bootstrap
;; - sorting and filtering a table - see existing libs
;; - tree-table

(def persons
  (r/atom [{:name "Popescu"
            :surname "Ion"
            :age 25
            :birthDate (js/Date. 1995 10 2)}
           {:name "Ionescu"
            :surname "Gheorghe"
            :age 30
            :birthDate (js/Date. 1990 4 1)}]))

(def nameToAcount
  (r/atom {"Popescu" [{:name "RON Account"
                       :balance 100}]
           "Ionescu" [{:name "EUR Account"
                       :balance 70}
                      {:name "RON Account"
                       :balance 50}]}))

(def persons-table-selection (r/atom #{}))
(def accounts-table-selection (r/atom #{}))
(def persons-forms-atom (r/atom nil))
(def accounts-forms-atom (r/atom nil))
(def tempNameToAcount (r/atom @nameToAcount))
(def tempNameToAcount-state (r/atom false))
(def ok-cancel-state (r/atom false))

(defn remove-person [pers-atom table-selection]
  (let [selection @table-selection
        names-to-search (vec (for [sel selection]
                             (:name sel)))
        pers @pers-atom]
    (remove (fn [p]
              (not= nil (some #{(:name p)} names-to-search)))
            pers)))

(defn persons-table []
  [:div.row
   (let [columnKeys [:name :surname :age :birthDate]
         columnLabels ["Name" "Surname" "Age" "Birth Date"]]
     [ui/table columnKeys columnLabels persons nil
      #(reset! persons-forms-atom (first (reverse @persons-table-selection)))
      persons-table-selection])])

(def persons-forms-template
  [:div.row
   [:div
    (ui/label "Name" {:class "label label-default"})
    (ui/single-line-string :name nil "Name")]
   [:div
    (ui/label "Surname" {:class "label label-default"})
    (ui/single-line-string :surname nil "Surname")]
   [:div
    (ui/label "Age" {:class "label label-default"})
    (ui/spinner :age 0 99 nil "Age")]
   [:div
    (ui/label "BirthDate" {:class "label label-default"})
    (ui/date-field :birthDate nil "BirthDate")]])

(defn persons-form [persons-forms-atom]
  [:div.forms
   [rf/bind-fields persons-forms-template persons-forms-atom]])

(defn buttons []
  [:div.row {:style {:margin-top "10px"}}
   (ui/button "Add" {:class "btn btn-default"} "https://cdn4.iconfinder.com/data/icons/simplicio/128x128/file_add.png"
              #(let [form-atom @persons-forms-atom]
                (do (swap! persons  (fn [p]
                                       (conj p form-atom)))
                    (swap! nameToAcount  (fn [nta]
                                            (merge nta {(:name form-atom) []})))
                    (reset! tempNameToAcount @nameToAcount)
                    (reset! persons-forms-atom nil))) "ADD")
   (ui/button "Edit" {:class "btn btn-default"} "https://cdn4.iconfinder.com/data/icons/simplicio/128x128/file_edit.png"
              #(reset! ok-cancel-state
                       true)
              "Edit")
   (ui/button "Remove" {:class "btn btn-default"} "https://cdn4.iconfinder.com/data/icons/simplicio/128x128/file_delete.png"
              #(let [updated-pers (remove-person persons persons-table-selection)]
                (do (reset! persons updated-pers)
                    (reset! persons-forms-atom nil)
                    (reset! persons-table-selection #{})))
              "Remove")])

;;;;;; Accounts

(defn add-account-to-person [nameToAcountAtom personsTableSelection accountsFormsAtom]
  (let [accounts (first (remove nil? (for [item @nameToAcountAtom]
                                       (if (= (:name (first @personsTableSelection))
                                              (first item))
                                         (second item)))))]
    (conj accounts @accountsFormsAtom)))

(defn remove-account-from-person [nameToAcountAtom personsTableSelection accountsTableSelection]
  (let [names-to-search (vec (for [sel @accountsTableSelection]
                               (:name sel)))]
    (first (remove nil? (for [item @nameToAcountAtom]
                          (if (= (:name (first @personsTableSelection))
                                 (first item))
                            (remove (fn [acc]
                                      (not= nil (some #{(:name acc)} names-to-search)))
                                    (second item))))))))

(defn edit-account-from-person [nameToAcountAtom personsTableSelection accountsTableSelection accountsFormsAtom]
  (let [accounts (first (remove nil? (for [item @nameToAcountAtom]
                                       (if (= (:name (first @personsTableSelection))
                                              (first item))
                                         (second item)))))]

    (vec (for [acc accounts]
           (if (= (:name acc) (:name (first @accountsTableSelection)))
             (merge acc @accountsFormsAtom)
             acc)))))

(defn updated-name-to-account [action nameToAcountAtom personsTableSelection accountsTableSelection accountsFormsAtom]
  (merge @nameToAcount
         {(:name (first @personsTableSelection))
          (if (= "add" action)
            (add-account-to-person nameToAcountAtom personsTableSelection accountsFormsAtom)
            (if (= "remove" action)
              (remove-account-from-person nameToAcountAtom personsTableSelection accountsTableSelection)
              (if (= "edit" action)
                (edit-account-from-person nameToAcountAtom personsTableSelection accountsTableSelection accountsFormsAtom))))}))

(defn accounts-table []
  [:div.row
   (let [selected-person (:name (first @persons-table-selection))
         selected-person-account (r/atom (first (remove nil? (for [item @nameToAcount]
                                                               (if (= selected-person (first item))
                                                                 (second item))))))
         columnKeys [:name :balance]
         columnLabels ["Name" "Balance"]]
     [ui/table columnKeys columnLabels selected-person-account nil
      #(reset! accounts-forms-atom (first (reverse @accounts-table-selection)))
      accounts-table-selection])])

(def accounts-forms-template
  [:div.row
   [:div
    (ui/label "Name" {:class "label label-default"})
    (ui/single-line-string :name nil "Name")]
   [:div
    (ui/label "Balance" {:class "label label-default"})
    (ui/spinner :balance nil nil nil "Balance")]])

(defn accounts-form [acc-table-selection]
  [:div.forms
   [rf/bind-fields accounts-forms-template accounts-forms-atom]])

(defn accounts-buttons []
  [:div.row {:style {:margin-top "10px"}}
   (ui/button "Add" {:class "btn btn-default"} "https://cdn4.iconfinder.com/data/icons/simplicio/128x128/file_add.png"
              #(do (if (= false @tempNameToAcount-state)
                     (do (reset! tempNameToAcount @nameToAcount)
                         (reset! tempNameToAcount-state true)))
                   (reset! nameToAcount (updated-name-to-account "add" nameToAcount persons-table-selection accounts-table-selection accounts-forms-atom))
                   (reset! accounts-forms-atom nil)) "ADD")
   (ui/button "Edit" {:class "btn btn-default"} "https://cdn4.iconfinder.com/data/icons/simplicio/128x128/file_edit.png"
              #(do (if (= false @tempNameToAcount-state)
                     (do (reset! tempNameToAcount @nameToAcount)
                         (reset! tempNameToAcount-state true)))
                   (reset! nameToAcount (updated-name-to-account "edit" nameToAcount persons-table-selection accounts-table-selection accounts-forms-atom))
                   (reset! accounts-forms-atom nil)
                   (reset! accounts-table-selection #{}))
              "Edit")
   (ui/button "Remove" {:class "btn btn-default"} "https://cdn4.iconfinder.com/data/icons/simplicio/128x128/file_delete.png"
              #(do (if (= false @tempNameToAcount-state)
                     (do (reset! tempNameToAcount @nameToAcount)
                         (reset! tempNameToAcount-state true)))
                   (reset! nameToAcount (updated-name-to-account "remove" nameToAcount persons-table-selection accounts-table-selection accounts-forms-atom))
                   (reset! accounts-forms-atom nil)
                   (reset! accounts-table-selection #{}))
              "Remove")])

(defn dialog []
  (ui/ok-cancel-dialog ok-cancel-state
                       "Accounts"
                       [:div {:style {:margin-bottom "3px"}}
                        [accounts-table]
                        [accounts-form accounts-table-selection]
                        [accounts-buttons]]
                       #(do (reset! tempNameToAcount-state false)
                            (reset! tempNameToAcount @nameToAcount)
                            (reset! accounts-table-selection #{})
                            (reset! persons-table-selection #{}))
                       #(do (reset! nameToAcount @tempNameToAcount)
                            (reset! tempNameToAcount-state false)
                            (reset! accounts-table-selection #{})
                            (reset! persons-table-selection #{}))))

(defn extended-demo-view []
  [:div.col-md-12
   [persons-table]
   [persons-form persons-forms-atom]
   [buttons]
   [dialog]])