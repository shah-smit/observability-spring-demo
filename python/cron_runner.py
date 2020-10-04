import schedule
import time
from datetime import datetime
from elasticsearchdemo import generate_report


def job(t):
    print("I'm working...", t)
    generate_report()
    return


schedule.every().day.at("23:30").do(job, datetime.now().strftime("%m/%d/%Y, %H:%M:%S"))


while True:
    print("In While... before pending")
    schedule.run_pending()
    print("In While... after pending")
    time.sleep(3600)  # wait one hour
    print("In While... after sleep")
