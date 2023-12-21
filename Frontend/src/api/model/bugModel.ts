export interface ErrorAndException {
  Error: number;
  Exception: number;
}

export interface BugListResult {
  list: Array<Bug>;
}

export interface Bug {
  name: string;
  value: number;
}
