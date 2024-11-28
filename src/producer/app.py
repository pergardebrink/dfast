import csv
import requests
import argparse


def main(file_path, merchant_id, endpoint_type):
    base_url = "http://localhost:4444/api"
    url = f"{base_url}/{merchant_id}/{endpoint_type}"
    session = requests.Session()

    with open(file_path, mode='r') as file:
        csv_reader = csv.DictReader(file)
        for row in csv_reader:
            session.post(url, json=row)
            print(".", end="", flush=True)

    print("\nAll data posted successfully.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Post CSV data to a specified endpoint.")
    parser.add_argument("file_path", type=str, help="Path to the CSV file to be processed")
    parser.add_argument("merchant_id", type=str, help="Merchant Id")
    parser.add_argument("endpoint_type", type=str, choices=["order", "inventory"], help="Type of data to send (order or inventory)")
    args = parser.parse_args()
    main(args.file_path, args.merchant_id, args.endpoint_type)
