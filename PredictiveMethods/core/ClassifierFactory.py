from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor
from sklearn.tree import DecisionTreeRegressor

from ClassifierWrapper import ClassifierWrapper


def get_classifier(method, n_estimators, max_features, gbm_learning_rate=None, random_state=None, min_cases_for_training=30):

    if method == "rf":
        return ClassifierWrapper(
            cls=RandomForestRegressor(n_estimators=n_estimators, max_features=max_features, random_state=random_state),
            min_cases_for_training=min_cases_for_training)
               
    elif method == "gbm":
        return ClassifierWrapper(
            cls=GradientBoostingRegressor(n_estimators=n_estimators, max_features=max_features, learning_rate=gbm_learning_rate, random_state=random_state),
            min_cases_for_training=min_cases_for_training)
    elif method == "dt":
        return ClassifierWrapper(
            cls=DecisionTreeRegressor(random_state=random_state),
            min_cases_for_training=min_cases_for_training)

    else:
        print("Invalid classifier type")
        return None