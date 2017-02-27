// file: modules/vpc/outputs.tf

output "id"                  { value = "${aws_vpc.main.id}" }
output "cidr_block"          { value = "${aws_vpc.main.cidr_block}" }
output "internal_subnet_ids" { value = ["${aws_subnet.internal.*.id}"] }
output "external_subnet_ids" { value = ["${aws_subnet.external.*.id}"] }
output "availability_zones"  { value = "${var.availability_zones}" }